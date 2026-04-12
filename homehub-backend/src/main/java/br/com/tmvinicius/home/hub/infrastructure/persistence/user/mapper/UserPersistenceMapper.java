package br.com.tmvinicius.home.hub.infrastructure.persistence.user.mapper;

import br.com.tmvinicius.home.hub.domain.model.user.Email;
import br.com.tmvinicius.home.hub.domain.model.user.Password;
import br.com.tmvinicius.home.hub.domain.model.user.User;
import br.com.tmvinicius.home.hub.infrastructure.persistence.user.UserPersistence;

public class UserPersistenceMapper {

    public UserPersistence domainToEntity(User user){
        return new UserPersistence(user.getId(),
                user.getEmail().getValue(),
                user.getPassword().getValue(),
                user.getRole(),
                user.getActive());
    }

    public User entityToDomain(UserPersistence userPersistence){
        Email email = new Email(userPersistence.getEmail());
        Password password = Password.fromHash(userPersistence.getPassword());

        return new User(userPersistence.getId(),
                email,
                password,
                userPersistence.getUserRole(),
                userPersistence.getActive());
    }


}
