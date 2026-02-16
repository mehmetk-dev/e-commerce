package com.mehmetkerem.security;

import com.mehmetkerem.enums.AuthProvider;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        // Google dışındaki sağlayıcıları şimdilik desteklemesek de genel yapı bu olur
        if (!"google".equals(provider)) {
            // Facebook vb. için logic eklenebilir
        }

        return processOAuth2User(oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Mevcut kullanıcıyı güncelle
            user.setName(name);
            user.setImageUrl(picture);
            // Eğer daha önce LOCAL ile girmişse, şimdi Google ID'sini de ekleyebiliriz
            // Veya sadece provider'ı değiştirebiliriz
            userRepository.save(user);
        } else {
            // Yeni kullanıcı oluştur
            user = User.builder()
                    .email(email)
                    .name(name)
                    .imageUrl(picture)
                    .provider(AuthProvider.GOOGLE)
                    .role(Role.USER) // Varsayılan rol
                    .passwordHash("") // Şifre yok
                    .build();
            userRepository.save(user);
        }

        return oAuth2User;
    }
}
