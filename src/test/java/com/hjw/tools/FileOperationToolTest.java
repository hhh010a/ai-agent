package com.hjw.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    private FileOperationTool fileOperationTool = new FileOperationTool();
    @Test
    void readFile() {
        System.out.println(fileOperationTool.readFile("test.txt"));
    }

    @Test
    void writeFile() {
        fileOperationTool.writeFile("test.txt","hello world");
    }
}
