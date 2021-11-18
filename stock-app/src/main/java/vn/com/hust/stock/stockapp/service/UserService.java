package vn.com.hust.stock.stockapp.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.com.hust.stock.stockapp.repository.UserRepository;
import vn.com.hust.stock.stockapp.sercurity.JwtTokenProvider;
import vn.com.hust.stock.stockmodel.exception.BusinessException;
import vn.com.hust.stock.stockmodel.exception.ErrorCode;
import vn.com.hust.stock.stockmodel.exception.PermissionException;
import vn.com.hust.stock.stockmodel.user.Role;
import vn.com.hust.stock.stockmodel.user.User;
import vn.com.hust.stock.stockmodel.user.UserDataDTO;

import java.util.ArrayList;
import java.util.Arrays;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    public String signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            return jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRoles());
        } catch (AuthenticationException e) {
            throw new PermissionException("Invalid username/password supplied", ErrorCode.EXPIRED_INVALID_TOKEN);
        }
    }

    public String signup(User user) {
        if (!user.getEmail().contains("@gmail.com"))
            throw new PermissionException("email need @gmail.com ", ErrorCode.EXPIRED_INVALID_TOKEN);
        if (!userRepository.existsByUsername(user.getUsername()) && !userRepository.existsByEmail(user.getEmail())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(new ArrayList<>(Arrays.asList(Role.ROLE_CLIENT)));
            userRepository.save(user);
            return jwtTokenProvider.createToken(user.getUsername(), user.getRoles());
        } else {
            throw new PermissionException("Username is already in use", ErrorCode.EXPIRED_INVALID_TOKEN);
        }
    }

    public User resetPass(UserDataDTO userDataDTO,HttpServletRequest request) {
           User user = this.whoami(request);
           user.setPassword(passwordEncoder.encode(userDataDTO.getPassword()));
           userRepository.save(user);
           return user;
    }

    public User approve(int id,Role role) {
       User user = userRepository.findById(id)
               .orElseThrow(()-> new BusinessException(ErrorCode.INVALID_USER_NAME));
       user.setRoles(new ArrayList<>(Arrays.asList(role)));
       userRepository.save(user);
       return user;
    }
    public Page<User> getAllUser(int page, int size) {
        return userRepository.findAll(PageRequest.of(page,size));
    }

    public void delete(String username) {
        userRepository.deleteByUsername(username);
    }

    public User search(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new PermissionException("The user doesn't exist",ErrorCode.EXPIRED_INVALID_TOKEN);
        }
        return user;
    }

    public User whoami(HttpServletRequest req) {
        return userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
    }

    public String refresh(String username) {
        return jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRoles());
    }
    public User updateUser(UserDataDTO userDataDTO,HttpServletRequest request) {
        User user = this.whoami(request);
        user.setFullName(userDataDTO.getFullName());
        user.setProfession(userDataDTO.getProfession());
        user.setInvestment(userDataDTO.getInvestment());
        user.setStrategy(userDataDTO.getStrategy());
        user.setTelephone(userDataDTO.getTelephone());
        userRepository.save(user);
        return user;
    }

}
