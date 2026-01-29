package archix_base.identity.service;

import archix_base.common.exception.ResourceNotFoundException;
import archix_base.identity.entity.User;
import archix_base.identity.repo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;














@Service
@AllArgsConstructor
public class UserDetailsServiceImp implements UserDetailsService {

    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByEmail(username).orElseThrow(
                ()->new ResourceNotFoundException("No user found with this email : "+username)
        );
    }
}









