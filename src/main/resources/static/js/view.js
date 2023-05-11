$(function(){
    getDashboard();
});
function getDashboard() {
    $.ajax({
        url: 'dashboard',
        method: 'GET',
        contentType: 'application/json;',
        dataType: 'json',
        error: function (error, status, msg) {
            alert("�����ڵ� " + status + "�����޽���" + msg);
        },
        success: function (data) {
            // console.log(data.data)
            var monthlyCost = document.getElementById('MonthlyCost');
            var instanceUsage = document.getElementById('InstanceUsage');
            var volumeUsage = document.getElementById('VolumeUsage');

            monthlyCost.textContent = '$' + data.data.estimatedCost;
            instanceUsage.textContent = data.data.instanceCnt;
            volumeUsage.textContent = data.data.volCnt;
            // console.log(data.data.estimatedCost)


        }

    });
}