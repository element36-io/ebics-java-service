package io.element36.cash36.ebics.strategy;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.element36.cash36.ebics.dto.StatementDTO;

public interface Statement {

    public List<StatementDTO> process(File z53OutFile) throws IOException;
    
}
