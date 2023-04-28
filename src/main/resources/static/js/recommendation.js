$(function(){
    // recommendation_Instance();
    getAccount();
});


let regionId;
let accountId;
let outDatedInstancesResult;
let outDatedInstancesContents;
let unusedInstancesResult;
let unusedInstancesContents;

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
                    console.log(result.data[0].instanceOptResponse.outDatedInstances[0])
                    console.log(result.data[0].instanceOptResponse.unusedInstances[0])
                    // recommendContents = result.data[0].instanceOptResponse.outDatedInstances
                    if(result.data[0].instanceOptResponse.outDatedInstances != null){
                        outDatedInstancesResult = ("오래된 인스턴스 있음")
                        outDatedInstancesContents = (result.data[0].instanceOptResponse.outDatedInstances[0].original.instanceType + "를 "
                            + result.data[0].instanceOptResponse.outDatedInstances[0].recommendation.instanceType + "으로 업그레이드 하세요")
                    }

                    else{
                        outDatedInstancesResult = ("인스턴스가 최신입니다")
                    }

                    if(result.data[0].instanceOptResponse.unusedInstances != null){
                        unusedInstancesResult = ("미사용 인스턴스 있음")
                        unusedInstancesContents = ("삭제시 절약 가능 비용 : " + result.data[0].instanceOptResponse.unusedInstances[0].estimatedMonthlySavings + "$")
                    }

                    else{
                        unusedInstancesResult = ("모든 인스턴스 사용중")
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
                                    + '<button id="show">추천내용</button>'
                                    + '<div class="background">'
                                    + '<div class="window">'
                                    + '<div class="popup">'
                                    + '<div id="close"> X </div>'
                                    + '<h5>추천 인스턴스</h5>'
                                    + '<div class="outDatedInstances">' + outDatedInstancesResult + '</div>'
                                    + '<div class="outDatedInstances">' + outDatedInstancesContents + '</div>'
                                    + '<div class="unusedInstances">' + unusedInstancesResult + '</div>'
                                    + '<div class="unusedInstances">' + unusedInstancesContents + '</div>'
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

function getInstance() {
    $.ajax({
        url: 'instance/get',
        method: 'GET',
        contentType: 'application/json;',
        dataType: 'json',
        error: function (error, status, msg) {
            alert("상태코드 " + status + "에러메시지" + msg);
        },
        success: function () {
            alert("인스턴스를 가져왔습니다")
        }

    });
}