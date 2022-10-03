$(function(){
    resource_list();
});

function resource_list(){
    $('#resource_usage_list').DataTable().ajax({
    // $.ajax({
        url : 'usage/resource',
        method : 'GET',
        contentType:'application/json;',
        dataType:'json',

        // error:function(error,status,msg){
        //     alert("상태코드" + status + "에러메세지" + msg );
        // },
        dataSrc:
            function resource_list_result(list){
                $(list).each(function(index, item) {
                    $.append('<tr><td>'
                        + item.accountName + '</td><td>'
                        + item.region + '</td><td>'
                        + item.resourceId + '</td><td>'
                        + item.resourceName + '</td><td>'
                        + item.os + '</td><td>'
                        + item.instanceType +'</td></tr>' );
                });
            }
    });
}

// function resource_list_result(list){
//     $(list).each(function(index, item) {
//         $('#resource_usage_list').append('<tr><td>'
//             + item.accountName + '</td><td>'
//             + item.region + '</td><td>'
//             + item.resourceId + '</td><td>'
//             + item.resourceName + '</td><td>'
//             + item.os + '</td><td>'
//             + item.instanceType +'</td></tr>' );
//     });
// }