package archix_base.services;

import archix_base.exceptions.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import archix_base.repo.UserRepo;

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
