jQuery(document).ready(function() {
    initialEven();

});
//初始化输入框事件
function initialEven() {
    $('.page-container .divform .username').keyup(function(){
        $('#userNm').fadeOut('fast');
    });
    $(' .page-container .divform .password').keyup(function(){
        $('#pwd').fadeOut('fast');
    });
    $(' .page-container .divform .Captcha').keyup(function(){
        $('#captcha').fadeOut('fast');
    });

    $('.submit_button').on('click',function(){
        formSubimt();
    });
    $('.kaptcha li').on('click',function(){
        $("#codeImg").attr("src", "http://localhost/udcs-web/kaptchaImage?t="+Math.random());
    });
    $("#codeImg").css('opacity','.3');//针对所有通用浏览器
    $("#codeImg").css('opacity','alpha(opacity=30)');//针对IE浏览器
}
//form提交事件
function  formSubimt() {
    var flag=true;
    var objForm=$('.page-container div');
    var username =objForm.find('.username').val();
    var password = objForm.find('.password').val();
    var captcha= objForm.find('.Captcha').val();
    if(username == undefined ||username == '') {
        objForm.find('#userNm').fadeOut('fast', function(){
            $('#userNm').css('display','inline-table');
            $('#userNm').css('top','25px');
        });
        objForm.find('#userNm').fadeIn('fast', function(){
            objForm.parent().find('.username').focus();
        });
        flag=false;
    }
    if(password == undefined ||password == '') {
        objForm.find('#pwd').fadeOut('fast', function(){
            $('#pwd').css('display','inline-table');
            $('#pwd').css('top','93px');
        });
        objForm.find('#pwd').fadeIn('fast', function(){
            objForm.parent().find('.password').focus();
        });
        flag=false;
    }
    if(captcha == undefined ||captcha == ''){
        objForm.find('#captcha').fadeOut('fast', function(){
            $('#captcha').css('display','inline-table');
            $('#captcha').css('top','162px');
        });
        objForm.find('#captcha').fadeIn('fast', function(){
            objForm.parent().find('.Captcha').focus();
        });
        flag=false;
    }
    if(flag){
        $.ajax({
            url:'/udcs-web/userLogin',
            type:'POST', //GET
            async:true,    //或false,是否异步
            data:{
                userName:username,pwd:password,captcha:captcha
            },
            timeout:5000,    //超时时间
            dataType:'json',    //返回的数据格式：json/xml/html/script/jsonp/text
            beforeSend:function(xhr){
                console.log(xhr)
                console.log('发送前')
            },
            success:function(data,textStatus,jqXHR){
                $.ligerDialog.alert('登录成功'+JSON.stringify(data), '登录成功', 'success');
                console.log(data)
                console.log(textStatus)
                console.log(jqXHR)
            },
            error:function(xhr,textStatus){
                $.ligerDialog.alert('登录失败'+textStatus, '登录失败', 'error');
                console.log('错误')
                console.log(xhr)
                console.log(textStatus)
            },
            complete:function(data){
                $.ligerDialog.alert('登录成功'+JSON.stringify(data), '登录成功', 'success');
                console.log('结束')
            }
        })
    }else{
        $.ligerDialog.alert('请输入完整的用户信息', '输入告警', 'warn');
    }
}
