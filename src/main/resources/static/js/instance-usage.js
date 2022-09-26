$(function(){
    resource_list();
});

function resource_list(){
    $.ajax({
        url : 'usage/resource',
        method : 'GET',
        contentType:'application/json;',
        dataType:'json',

        error:function(error,status,msg){
            alert("상태코드 " + status + "에러메시지" + msg );
        },
        success:resource_list_result
    });
}

function resource_list_result(list){
    $(list).each(function(index, item) {
        $('tbody#resource_usage_list').append('<tr><td>'
            // + item.accountId + '</td><td>'
            + item.accountName + '</td><td>'
            + item.region + '</td><td>'
            + item.resourceId + '</td><td>'
            + item.resourceName + '</td><td>'
            + item.os + '</td><td>'
            + item.instanceType );
    });
}