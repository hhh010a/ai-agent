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
            if(think()){
                return act();
            }
            return "思考结束--无需行动";
        } catch (Exception e) {
            return "发生错误："+e.getMessage();
        }
    }

    public abstract boolean think();

    public abstract String act();
}
