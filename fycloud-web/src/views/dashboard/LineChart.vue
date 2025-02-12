<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script>
import echarts from 'echarts'
require('echarts/theme/macarons') // echarts theme
import resize from './mixins/resize'

export default {
  mixins: [resize],
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '350px'
    },
    autoResize: {
      type: Boolean,
      default: true
    },
    chartData: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      chart: null,
      userProject: {
        sevenRunStatic: [
          [],
          [],
          [],
          []
        ]
      }
    }
  },
  watch: {
    chartData: {
      deep: true,
      handler(val) {
        this.setOptions(val)
      }
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart()
    })
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    initChart() {
      setTimeout(() => {
        this.userProject = JSON.parse(sessionStorage.getItem('userProject'))
      }, 500)
      this.chart = echarts.init(this.$el, 'macarons')
      setTimeout(() => {
        this.setOptions(this.chartData)
      }, 1000)
    },
    setOptions({ expectedData, actualData } = {}) {
      this.chart.setOption({
        title: {
          text: '最近一周用例执行情况'
        },
        xAxis: {
          data: this.lastSevenday(),
          boundaryGap: false,
          axisTick: {
            show: false
          }
        },
        grid: {
          left: 10,
          right: 10,
          bottom: 20,
          top: 30,
          containLabel: true
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          },
          padding: [5, 10]
        },
        yAxis: {
          axisTick: {
            show: false
          }
        },
        legend: {
          data: ['成功', '失败', '异常']
        },
        series: [{
          name: '成功', itemStyle: {
            normal: {
              color: '#32CD32',
              lineStyle: {
                color: '#32CD32',
                width: 2
              }
            }
          },
          smooth: true,
          type: 'line',
          data: this.userProject.sevenRunStatic[0],
          animationDuration: 2800,
          animationEasing: 'cubicInOut'
        },
        {
          name: '失败', itemStyle: {
            normal: {
              color: '#A52A2A',
              lineStyle: {
                color: '#A52A2A',
                width: 2
              }
            }
          },
          smooth: true,
          type: 'line',
          data: this.userProject.sevenRunStatic[1],
          animationDuration: 2800,
          animationEasing: 'cubicInOut'
        },
        {
          name: '异常',
          smooth: true,
          type: 'line',
          itemStyle: {
            normal: {
              color: '#CDAD00',
              lineStyle: {
                color: '#CDAD00',
                width: 2
              },
              areaStyle: {
                color: '#f3f8ff'
              }
            }
          },
          data: this.userProject.sevenRunStatic[2],
          animationDuration: 2800,
          animationEasing: 'quadraticOut'
        }]
      })
    },
    lastSevenday() {
      const sevenDays = []
      for (let i = 6; i >= 0; i--) {
        // 获取当前日期
        const today = new Date()
        const sevenDaysAgo = new Date(today)
        sevenDaysAgo.setDate(today.getDate() - i)
        sevenDays.push(sevenDaysAgo.toISOString().slice(0, 10))
      }
      return sevenDays
    }
  }
}
</script>
