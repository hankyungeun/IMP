$(function(){
    resource_list();
});

//recommendation 표
function resource_list(){
    // $('#resource_usage_list').DataTable().ajax({
    $('#resource_usage_list').DataTable({
        ajax:{
            url : 'usage/resource',
            type : 'GET',
            contentType:'application/json;',
            dataType:'json',
            dataSrc: ''
        },
    // $.ajax({
    //     url : 'usage/resource',
    //     method : 'GET',
    //     contentType:'application/json;',
    //     dataType:'json',
        columns :[
            { data:"accountName"},
            { data:"region" },
            { data:"resourceId"},
            { data:"resourceName"},
            { data:"os"},
            { data:"instanceType"}

        ]

        // error:function(error,status,msg){
        //     alert("상태코드" + status + "에러메세지" + msg );
        // },
        // success:
        //     function resource_list_result(list){
        //         $(list).each(function(index, item) {
        //             $('#resource_usage_list').dataTable().append('<tr><td>'
        //                 + item.accountName + '</td><td>'
        //                 + item.region + '</td><td>'
        //                 + item.resourceId + '</td><td>'
        //                 + item.resourceName + '</td><td>'
        //                 + item.os + '</td><td>'
        //                 + item.instanceType +'</td></tr>' );
        //         });
        //     }
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