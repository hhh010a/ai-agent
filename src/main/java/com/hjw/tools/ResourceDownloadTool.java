package com.hjw.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.hjw.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class ResourceDownloadTool {

    @Tool(description = " Download the resource file from the specified URL to the local system")
    public String resourceDownload(@ToolParam(description = "The complete URL of the resource file") String url,
                                   @ToolParam(description = "The name of the resource file") String fileName){
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
