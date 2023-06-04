// Set new default font family and font color to mimic Bootstrap's default styling
Chart.defaults.global.defaultFontFamily = 'Nunito', '-apple-system,system-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif';
Chart.defaults.global.defaultFontColor = '#858796';

function number_format(number, decimals, dec_point, thousands_sep) {
  // *     example: number_format(1234.56, 2, ',', ' ');
  // *     return: '1 234,56'
  number = (number + '').replace(',', '').replace(' ', '');
  var n = !isFinite(+number) ? 0 : +number,
      prec = !isFinite(+decimals) ? 0 : Math.abs(decimals),
      sep = (typeof thousands_sep === 'undefined') ? ',' : thousands_sep,
      dec = (typeof dec_point === 'undefined') ? '.' : dec_point,
      s = '',
      toFixedFix = function(n, prec) {
        var k = Math.pow(10, prec);
        return '' + Math.round(n * k) / k;
      };
  // Fix for IE parseFloat(0.55).toFixed(0) = 0;
  s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.');
  if (s[0].length > 3) {
    s[0] = s[0].replace(/\B(?=(?:\d{3})+(?!\d))/g, sep);
  }
  if ((s[1] || '').length < prec) {
    s[1] = s[1] || '';
    s[1] += new Array(prec - s[1].length + 1).join('0');
  }
  return s.join(dec);
}

// Set new default font family and font color to mimic Bootstrap's default styling
Chart.defaults.global.defaultFontFamily = '-apple-system,system-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif';
Chart.defaults.global.defaultFontColor = '#292b2c';


var chartArea2 = {
  labels : [],
  dataSets : [],
  diskData : [],
  memData : [],
  render : function() {
    new Chart($("#dateChart"), {
      type: 'line',
      data: {
        labels: chartArea2.labels,
        datasets: [{
          label: "CPU",
          fill : false,
          lineTension: 0.3,
          backgroundColor: 'rgb(255,192,203)',
          borderColor: 'rgb(255, 192, 203)',
          pointRadius: 5,
          pointBackgroundColor: "rgb(255,187,36)",
          pointBorderColor: "rgba(255,255,255,0.8)",
          pointHoverRadius: 5,
          pointBackgroundColor: "rgb(255,176,189)",
          pointHitRadius: 50,
          pointBorderWidth: 2,
          data: chartArea2.dataSets
        }],
      },

      options: {
        maintainAspectRatio: false,
        layout: {
          padding: {
            left: 10,
            right: 25,
            top: 25,
            bottom: 0
          }
        },
        responsive: true,
        scales: {
          xAxes: [{
            time: {
              unit: 'date'
            },
            gridLines: {
              display: false
            },
            ticks: {
              maxTicksLimit: 7
            }
          }],
          yAxes: [{
            gridLines: {
              color: "rgba(0, 0, 0, .125)",
            }
          }],
        },
        legend: {
          // position: 'left'
        }
      }
    });
  },

  showData : function(dateFrom, dateTo) {  // dateFrom, dateTo를 매개변수로 받도록 수정
    $.ajax({
      type : 'GET',
      url : 'usage/summaries?dateFrom='+dateFrom+'&dateTo='+dateTo,
      contentType: 'application/json',
      dataType: 'json',
      success : function(data) {
        let dates = [];
        let values = [];

        function getObjectLength(obj) {
          return Object.keys(obj).length;
        }

        var length = getObjectLength(data.data.cpuDailyAvgs);


        for (var i = 0; i < length; i++) {
          var date = Object.keys(data.data.cpuDailyAvgs)[i];
          var value = data.data.cpuDailyAvgs[date];
          console.log("ssss");
          dates.push(date);
          values.push(value);
          console.log(dates);
          console.log(values);
          chartArea2.labels.push(date); // dates 배열 대신에 date를 push
          chartArea2.dataSets.push(value);
        }

        chartArea2.render();
      },
      error : function(xhr, status, error){
      }
    });
  }
};

$(document).ready(function() { // DOM이 준비되면 실행되도록 추가
  $('#submitBtn').on('click', function () {
    var dateFrom = $('#dateFromInput').val();
    var dateTo = $('#dateToInput').val();
    chartArea2.labels = [];
    chartArea2.dataSets = [];
    chartArea2.showData(dateFrom, dateTo);
  });
});
