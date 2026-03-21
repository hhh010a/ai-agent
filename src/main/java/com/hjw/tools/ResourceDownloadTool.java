package com.hjw.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.hjw.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class ResourceDownloadTool {

    @Tool(description = " resource download")
    public String resourceDownload(@ToolParam(description = "resource url") String url,
                                   @ToolParam(description = "file name") String fileName){
        String fileDir=FileConstant.FILE_PATH + "/download";
        String filePath = fileDir+ "/"+fileName;
        try {
            FileUtil.mkdir(fileDir);
            HttpUtil.downloadFile(url, filePath);
            return "download success";
        } catch (Exception e) {
            return "download fail"+ e.getMessage();
        }
    }
}
