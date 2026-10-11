<template>
  <Header />
  <Sidebar />
  <div class="main_contents">
    <router-view />
  </div>
  <Footer />
</template>

<script setup>
import Header from "@/components/Header.vue";
import Footer from "@/components/Footer.vue";
import Sidebar from "@/components/Sidebar.vue";
</script>
<style scoped>
@import "../assets/css/mainLayout.css";
</style>
