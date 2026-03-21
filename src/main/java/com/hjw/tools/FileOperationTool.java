package com.hjw.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.hjw.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class FileOperationTool {

    private static final String FILE_DIR= FileConstant.FILE_PATH+"/file";

    @Tool(description = "File reading")
    public String readFile(@ToolParam(description = "File name to read") String fileName){
        String filePath = FILE_DIR+"/"+fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "Error read file" + e.getMessage();
        }
    }

    @Tool(description = "File writing")
    public String writeFile(@ToolParam(description = "File name to write") String fileName,
                            @ToolParam(description = "File content to write") String content){
        try {
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content,FILE_DIR+"/"+fileName);
            return "Write file success";
        } catch (Exception e) {
            return "Error write file"+ e.getMessage();
        }
    }
}
