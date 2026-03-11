package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

// gid == 0 means remove the tile at position.
// gid > 0 means add or replace the tile at position.
public record EditorLevelDTO(Position position, int gid) {}