<template>
    <h1>Oauth Page</h1>
</template>
    
<script>
import router from "@/router";
 
export default {
  created() {
    const atoken = this.$route.query.atoken
    const rtoken = this.$route.query.rtoken
    console.log('token',atoken)
    console.log('token',rtoken)
    if (atoken != null & rtoken != null){
        sessionStorage.setItem("access-token", atoken);
        sessionStorage.setItem("refresh-token", rtoken);
        window.alert('로그인하였습니다');
        router.push({path:'/project-list'});
    } else {
      window.alert('로그인에 실패하였습니다.')
      router.push({path:'/login'})
    }
  }
}
</script>
