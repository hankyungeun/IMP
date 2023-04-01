// Set new default font family and font color to mimic Bootstrap's default styling
Chart.defaults.global.defaultFontFamily = 'Nunito', '-apple-system,system-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif';
Chart.defaults.global.defaultFontColor = '#858796';

// Pie Chart Example

// var pieChart = {
//   datasets: [],
//   kind: [],
//   render: function () {
//     new Chart($("#myPieChart"), {
//       type: 'pie',
//       data: {
//         labels: pieChart.kind,
//         datasets: [{
//           data: pieChart.datasets,
//           backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc'],
//           hoverBackgroundColor: ['#2e59d9', '#17a673', '#2c9faf'],
//           hoverBorderColor: "rgba(234, 236, 244, 1)",
//         }],
//       },
//       options: {
//         maintainAspectRatio: false,
//         tooltips: {
//           backgroundColor: "rgb(255,255,255)",
//           bodyFontColor: "#858796",
//           borderColor: '#dddfeb',
//           borderWidth: 1,
//           xPadding: 15,
//           yPadding: 15,
//           displayColors: false,
//           caretPadding: 10,
//         },
//         legend: {
//           display: false
//         },
//         cutoutPercentage: 80,
//       },
//     });
//   },
//   showData: function () {
//     datasets= [];
//     kind= [];
//
//     $.ajax({
//       type: 'GET',
//       url: 'usage/state',
//       contentType: 'application/json',
//       dataType: 'json',
//       success: function (data) {
//
//         $.each(data, function (index, obj) {
//
//           console.log(obj.running);
//           // pieChart.labels.push(obj.running);
//           pieChart.dataSets.push(obj.running);
//
//         });
//         pieChart.render();
//       },
//       error : function(xhr, status, error){
//       }
//     });
//   }
// };
//
// pieChart.showData();



var pieChart = {
  chartLabels : [],
  chartData1 : [],
  chartData2 : [],
  render : function() {
    new Chart($("#myPieChart"), {
      type: 'pie',
      data: {
        labels: pieChart.chartLabels,
        datasets: [{
          label: "pieChart",
          fillColor: "rgba(220,220,220,0.2)",
          strokeColor: "rgba(220,220,220,1)",
          pointColor: "rgba(220,220,220,1)",
          pointStrokeColor: "#fff",
          pointHighlightFill: "#fff",
          pointHighlightStroke: "rgba(220,220,220,1)",
          data: pieChart.chartData1,
          backgroundColor: [
            "#FFC0CB",
            "#4BC0C0",
            "#FFCE56",
            "#E7E9ED",
            "#8ccfff"
          ]
        }],
      },
      options: {
        maintainAspectRatio: false,
        legend: {
          position: 'bottom'
        }
      }
    });
  },

  showData : function(){

    $.ajax({
      type : 'GET',
      url : "usage/state",
      contentType : 'application/json',
      dataType: 'json',
      error : function(XMLHttpRequest, textStatus, errorThrown) {
      },
      success : showData_result
    });
  }
} ;
function showData_result(data) {
  $.each(data, function(inx, obj) {
    if(inx == "running"){
      pieChart.chartLabels.push(inx);
      pieChart.chartData1.push(obj);
    }
    if(inx == "stopped"){
      pieChart.chartLabels.push(inx);
      pieChart.chartData1.push(obj);
    }
    if(inx == "terminated"){
      pieChart.chartLabels.push(inx);
      pieChart.chartData1.push(obj);
    }
  });

  pieChart.render();
}
pieChart.showData();
