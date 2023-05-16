// 사용 인스턴스 / 볼륨 목록get
$(function(){
    volume_list();
    instance_list();
});

function volume_list(){
    $.ajax({
        url : 'volume',
        method : 'GET',
        contentType:'application/json;',
        dataType:'json',

        error:function(error,status,msg){
            alert("상태코드 " + status + "에러메시지" + msg );
        },
        success:volume_list_result
    });
}

function volume_list_result(list){
    $(list).each(function(index, item) {
        $('tbody#volumeList').append('<tr><td>'
//                    + item.volumeId +'</td><td>'
            + item.volumeType + '</td><td>'
            + item.availabilityZone + '</td><td>'
            + item.size + '</td><td>'
            + item.state + '</td><td>'
            + item.createTime);
    });
}


function instance_list(){
    $.ajax({
        url : 'instance',
        method : 'GET',
        contentType:'application/json;',
        dataType:'json',

        error:function(error,status,msg){
            alert("상태코드 " + status + "에러메시지" + msg );
        },
        success:instance_list_result
    });
}

function instance_list_result(list){
    $(list).each(function(index, item) {
        $('#instanceList').append('<tr><td>'
            + item.instanceType + '</td><td>'
            + item.availabilityZone + '</td><td>'
            + item.os + '</td><td>'
            + item.instanceState + '</td><td>'
            + item.registered);
    });
}