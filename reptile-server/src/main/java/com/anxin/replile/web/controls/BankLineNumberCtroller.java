package com.anxin.replile.web.controls;

import com.anxin.replile.services.BankLineNumberService;
import com.anxin.replile.services.BankNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: ${FILE_NAME}
 * @Package com.anxin.replile.web.controls
 * @ClassName: ${TYPE_NAME}
 * @Description: ${TODO}(用一句话描述该文件做什么)
 * @date 2017/6/2 14:54
 */
@RestController
public class BankLineNumberCtroller {

    @Autowired
    private BankLineNumberService bankLineNbrService;

    @Autowired
    private BankNumberService bankNumberService;
    @RequestMapping("/bankLineNbr")
    @ResponseBody
    public String iniBankLineNbrData(){
        bankLineNbrService.iniBankLineNbrData();
        return "true";
    }

    @RequestMapping("/bankNbr")
    @ResponseBody
    public String iniBankNbrData(){
        bankNumberService.iniBankNumberData();
        return "true";
    }
}
