package com.hjw.tools;

import cn.hutool.core.io.FileUtil;
import com.hjw.constant.FileConstant;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class PDFGenerationTool {

    private static final String FILE_DIR= FileConstant.FILE_PATH+"/pdf";

    @Tool(description = "Generate PDF")
    public String generatePDF(@ToolParam(description = "Text to generate PDF") String text,
                              @ToolParam(description = "File name") String fileName){

        try {
            FileUtil.mkdir(FILE_DIR);
            // 1. 创建PdfWriter，指定输出路径
            PdfWriter writer = new PdfWriter(FILE_DIR+"/"+fileName);
            // 2. 创建PdfDocument
            PdfDocument pdf = new PdfDocument(writer);
            // 3. 创建Document（高级布局对象）
            Document document = new Document(pdf);
            // 4. 添加段落
            document.add(new Paragraph(text));
            // 5. 关闭文档
            document.close();
            return "PDF generated successfully";
        } catch (Exception e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }
}
