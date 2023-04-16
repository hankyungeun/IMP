$(function(){
    // recommendation_Instance();
    getAccount();
});


let regionId;
let accountId;
let recommendContents;
let outDatedInstancesresult;
let unusedInstancesresult

function getAccount() {
    $.ajax({
        url: 'user-info',
        method: 'GET',
        contentType: 'application/json;',
        dataType: 'json',

        error: function (error, status, msg) {
            alert("상태코드 " + status + "에러메시지" + msg);
        },
        success: function (data) {
            accountId = data.data.accounts[0].accountId
            regionId = data.data.accounts[0].regions

            $.ajax({
                url: 'optimizer?accountId=' + accountId + '&regionId=' + regionId,
                method: 'GET',
                contentType: 'application/json;',
                dataType: 'json',
                error: function (error, status, msg) {
                    alert("상태코드 " + status + "에러메시지" + msg);
                },
                success: function (result) {
                    console.log("계정 아이디 : " + accountId)
                    console.log("지역 : " + regionId)
                    console.log(result.data[0].instanceOptResponse.unusedInstances)
                    // recommendContents = result.data[0].instanceOptResponse.outDatedInstances
                    if(result.data[0].instanceOptResponse.outDatedInstances != null){
                        outDatedInstancesresult = ("오래된 인스턴스 있음")
                    }

                    else{
                        outDatedInstancesresult = ("인스턴스가 최신입니다")
                    }

                    if(result.data[0].instanceOptResponse.unusedInstances != null){
                        unusedInstancesresult = ("미사용 인스턴스 있음")
                    }

                    else{
                        unusedInstancesresult = ("모든 인스턴스 사용중")
                    }

                    $.ajax({
                        url: 'instance',
                        method: 'GET',
                        contentType: 'application/json;',
                        dataType: 'json',
                        error: function (error, status, msg) {
                            alert("상태코드 " + status + "에러메시지" + msg);
                        },
                        success: function instance_list_result(list){
                            $(list).each(function (index, item) {
                                $('#instanceList').append('<tr><td>'
                                    + item.instanceType + '</td><td>'
                                    + item.availabilityZone + '</td><td>'
                                    + item.os + '</td><td>'
                                    + item.instanceState + '</td><td>'
                                    + item.registered + '</td><td>'
                                    + '<button id="show">버튼</button>'
                                    + '<div class="background">'
                                    + '<div class="window">'
                                    + '<div class="popup">'
                                    + '<div id="close"> X </div>'
                                    + '<h5>추천 인스턴스</h5>'
                                    + '<div class="outDatedInstances">' + outDatedInstancesresult + '</div>'
                                    + '<div class="unusedInstances">' + unusedInstancesresult + '</div>'
                                    + '</div></div></div>'
                                )
                            })
                        }
                    });
                }
            });
        }
    });
}
window.onload = function (){
    function show () {
        document.querySelector(".background").className = "background show";
    }
    function close () {
        document.querySelector(".background").className = "background";
    }
    // 모달창 리스너
    document.querySelector("#show").addEventListener('click', show);
    document.querySelector("#close").addEventListener('click', close);
}