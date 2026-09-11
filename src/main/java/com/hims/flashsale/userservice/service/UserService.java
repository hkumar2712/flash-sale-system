package com.hims.flashsale.userservice.service;

import com.hims.flashsale.userservice.dto.RegisterRequest;
import com.hims.flashsale.userservice.dto.RegisterResponse;
import com.hims.flashsale.userservice.entity.User;
import com.hims.flashsale.userservice.exception.EmailAlreadyExistsException;
import com.hims.flashsale.userservice.repository.UserRepository;
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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}