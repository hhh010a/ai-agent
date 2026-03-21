package com.hjw.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDownloadToolTest {

    @Test
    void resourceDownload() {
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        System.out.println(resourceDownloadTool.resourceDownload("https://i0.hdslb.com/bfs/new_dyn/b9f501c192b5d813cd4e1e3347220113401742377.jpg@1052w_!web-dynamic.avif", "1.png"));
    }
}
