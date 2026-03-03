package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;

public record UserDTO(String id, String name) {

  public UserDTO(User user) {
    this(user.getId(), user.getName());
  }

}
