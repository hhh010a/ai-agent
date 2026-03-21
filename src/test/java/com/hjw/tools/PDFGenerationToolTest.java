package com.hjw.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        System.out.println(pdfGenerationTool.generatePDF("Hello World", "helloworld.pdf"));
    }
}
