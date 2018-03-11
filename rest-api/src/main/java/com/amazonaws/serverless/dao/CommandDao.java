package com.amazonaws.serverless.dao;

import com.amazonaws.serverless.domain.Command;

public interface CommandDao {
	Command getCommand();

    void addCommand(Command command);
}
