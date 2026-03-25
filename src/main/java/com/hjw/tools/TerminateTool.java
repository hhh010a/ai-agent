package com.hjw.tools;

import org.springframework.ai.tool.annotation.Tool;

public class TerminateTool {

    @Tool(description = """  
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.  
            "When you have finished all the tasks, call this tool to end the work.  
            """)
    public String terminate(){
        return "任务结束";
    }
}
