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
        ],
        errorTopNameStatic: null
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
          text: '最近一周失败TOP10'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {},
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          boundaryGap: [0, 0.01]
        },
        yAxis: {
          type: 'category',
          data: this.userProject.errorTopNameStatic
        },
        series: [
          {
            itemStyle: {
              color: '#FF3030'
            },
            type: 'bar',
            data: this.userProject.sevenRunStatic[3]
          }
        ]
      })
    }
  }
}
</script>
