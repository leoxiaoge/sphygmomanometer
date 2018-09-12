//app.js
App({
  onLaunch: function () {
    //调用API从本地缓存中获取数据
    var logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)
  },
  getUserInfo:function(cb){
    var that = this
    if(this.globalData.userInfo){
      typeof cb == "function" && cb(this.globalData.userInfo)
    }else{
      //调用登录接口
      wx.login({
        success: function () {
          wx.getUserInfo({
            success: function (res) {
              that.globalData.userInfo = res.userInfo
              typeof cb == "function" && cb(that.globalData.userInfo)
            }
          })
        }
      })
    }
  },
  globalData:{
    userInfo:null,
    ishow:0,
    deviceId: '',//连接的主设备Id.ble设备是mac地址
    bettery:0,//连接的主设备的电量信息
    blecurrent:0,//电量的电流,0是默认,小于50说明断开了!
    service: {},//连接的主设备提供的所有服务UUID
    isnotice:0,//是否提醒过电量过低
    isothernotice: 0,//是否提醒过电流
    characteristic: {},//连接的主设备的特征值UUID
  },
  url: 'https://api.shixinshuma.com',
  login: function (eapi,that) { //用户登入
    wx.login({
      success: function (loginCode) {
        wx.request({
          url: eapi + '/api/v1/login/wxauth',
          data: {
            code: loginCode.code
          },
          method: 'POST', // OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT
          // header: {}, // 设置请求的 header
          success: function(res){
            // success
            wx.setStorageSync('openid', res.data.data.openid);//缓存openid
          },
          fail: function() {
            // fail
          },
          complete: function() {
            // complete
          }
        })
      }
    })
  },
})