package com.hjw.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent{

    public String step(){
        try {
            return think();
        } catch (Exception e) {
            return "发生错误："+e.getMessage();
        }
    }

    public abstract String think();

    public abstract String act();
}
