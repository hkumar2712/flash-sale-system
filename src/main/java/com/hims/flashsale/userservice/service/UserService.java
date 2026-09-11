package com.hims.flashsale.userservice.service;

import com.hims.flashsale.userservice.dto.LoginRequest;
import com.hims.flashsale.userservice.dto.LoginResponse;
import com.hims.flashsale.userservice.dto.RegisterRequest;
import com.hims.flashsale.userservice.dto.RegisterResponse;
import com.hims.flashsale.userservice.entity.User;
import com.hims.flashsale.userservice.exception.EmailAlreadyExistsException;
import com.hims.flashsale.userservice.exception.InvalidCredentialsException;
import com.hims.flashsale.userservice.repository.UserRepository;
import com.hims.flashsale.userservice.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @Service marks this as a Spring-managed bean belonging to the "service layer" -
 * the layer that holds BUSINESS LOGIC, sitting between controllers (HTTP concerns)
 * and repositories (persistence concerns). Controllers should stay thin and just
 * translate HTTP <-> method calls; repositories should stay thin and just do
 * database access. Rules like "email must be unique" or "hash the password before
 * saving" belong HERE, not scattered into either of those other layers.
 *
 * Constructor injection (below) rather than @Autowired on fields: this is the
 * Spring-recommended approach - it makes dependencies explicit and required
 * (the class literally cannot be constructed without them), and makes writing
 * unit tests trivial later (just pass in mocks via the constructor, no Spring
 * container needed at all for a plain unit test).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long jwtExpirationMs;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${jwt.expiration-ms}") long jwtExpirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public RegisterResponse register(RegisterRequest request) {
        // Business rule #1: no duplicate emails. We check explicitly here rather
        // than relying solely on the DB's unique constraint, so we can return a
        // clean, specific error instead of a raw SQL constraint-violation exception
        // leaking up to the client.
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // Business rule #2: never store the raw password. encode() runs it through
        // BCrypt, producing a one-way hash - this is the ONLY place in the entire
        // application that should ever touch a plaintext password.
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // TODO(security): currently ANY caller can register as ADMIN by simply
        // setting role=ADMIN in the request body. Revisit before this goes anywhere
        // near production - e.g. restrict ADMIN creation to an authenticated admin
        // action, or a separate internal-only endpoint.
        User user = new User(request.getEmail(), hashedPassword, request.getRole());

        User saved = userRepository.save(user);

        return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getRole(), saved.getCreatedAt());
    }

    public LoginResponse login(LoginRequest request) {
        // Look up by email first. Note: we do NOT reveal at this point whether the
        // email exists or not - both "email not found" and "password mismatch" below
        // throw the exact same InvalidCredentialsException, with the exact same message.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        // matches() re-hashes the RAW password using the same BCrypt algorithm/salt
        // stored in the existing hash, then compares the result. We never "decrypt"
        // the stored hash - BCrypt is one-way by design; this is the only valid way
        // to check a password against it.
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, jwtExpirationMs);
    }
}