<template>
  <div class="canvas_container">
    <div class="canvas_info_container">
      <p>Start Simply and Swiftly</p>
      <img
        class="canvas_img hidden"
        src="../assets/images/main_logo.png"
        alt=""
      />
      <div class="canvas_info_container_main">
        <h2>쉽고 빠른</h2>
        <h2>개발 프로젝트 관리 서비스</h2>
        <h2 id="fontUp">AM_PM</h2>
        <h4 class="margintop20">
          초보자부터 숙련된 개발자까지 안전하고 편리한
        </h4>
        <h4>최고의 개발 환경을 경험해보세요.</h4>
        <router-link to="/login">
          <button class="canvaas_login_btn hidden">START</button>
        </router-link>
      </div>
    </div>
    <canvas></canvas>
    <svg class="svg_container">
      <defs>
        <filter id="gooey">
          <feGaussianBlur in="SourceGraphic" stdDeviation="40" result="blur1" />
          <feColorMatrix
            in="blur1"
            mode="matrix"
            values="1 0 0 0 0  0 1 0 0 0  0 0 1 0 0  0 0 0 100 -23"
          />
        </filter>
      </defs>
    </svg>
  </div>
  <div class="main_container">
    <MainContent></MainContent>
  </div>
  <Footer class="mainpage_footer"></Footer>
</template>

<script setup>
import MainContent from "@/components/MainPage/MainContent.vue";
import { onMounted } from "vue";
import Footer from "@/components/Footer.vue";
window.scrollTo(0, 0);

onMounted(() => {
  const mainLogo = document.querySelector(".canvas_img");
  const mainBtn = document.querySelector(".canvaas_login_btn");
  const canvas = document.querySelector("canvas");
  const ctx = canvas.getContext("2d");
  const dpr = window.devicePixelRatio;

  setTimeout(() => {
    mainLogo.classList.remove("hidden");
    mainBtn.classList.remove("hidden");
  }, 3000);

  let canvasWidth;
  let canvasHeight;

  const randomNumBetween = (min, max) => {
    return Math.random() * (max - min + 1) + min;
  };
  let particles;

  let interval = 1000 / 60;
  let now, delta;
  let then = Date.now();

  class Particle {
    constructor(x, y, radius, vy) {
      this.x = x;
      this.y = y;
      this.radius = radius;
      this.vy = vy;
      this.acc = 1.01;
    }
    update() {
      this.vy *= this.acc;
      this.y -= this.vy; // 파티클을 위로 이동
    }
    draw() {
      ctx.beginPath();
      ctx.arc(this.x, this.y, this.radius, 0, (Math.PI / 180) * 360);
      ctx.fillStyle = "#166ADC";
      //   #166ADC
      ctx.fill();
      ctx.closePath();
    }
  }

  function init() {
    canvasWidth = innerWidth;
    canvasHeight = innerHeight;

    canvas.width = canvasWidth * dpr;
    canvas.height = canvasHeight * dpr;
    ctx.scale(dpr, dpr);

    canvas.style.width = canvasWidth + "px";
    canvas.style.height = canvasHeight + "px";

    particles = [];
    const TOTAL = canvasWidth / 5;

    for (let i = 0; i < TOTAL; i++) {
      const x = randomNumBetween(0, canvasWidth);
      const y = canvasHeight - randomNumBetween(0, canvasHeight / 2); // 바닥 근처에서 초기화
      const radius = randomNumBetween(0, 5);
      const vy = randomNumBetween(10, 20);
      const particle = new Particle(x, y, radius, vy);
      particles.push(particle);
    }
    a = 0;
    b = 0;
  }
  let a = 0;
  let b = 0;
  function animate() {
    b++;
    if (a > 300) {
      if (b < 200) {
        window.requestAnimationFrame(animate);
      }
      particles.forEach((particle) => {
        particle.update();
        particle.draw();
      });
      return;
    }
    window.requestAnimationFrame(animate);
    now = Date.now();
    delta = now - then;

    if (delta < interval) return;

    ctx.clearRect(0, 0, canvasWidth, canvasHeight);

    particles.forEach((particle) => {
      particle.update();
      particle.draw();
      if (particle.y + particle.radius < 0) {
        particle.y = canvasHeight + a; // 캔버스 위쪽으로 이동하면 다시 아래쪽에서 시작
        if (a < 900) {
          a++;
        }
        particle.radius = randomNumBetween(50, 100);
        particle.vy = randomNumBetween(1, 5);
      }
    });

    then = now - (delta % interval);
  }

  window.addEventListener("load", () => {
    init();
    animate();
  });

  window.addEventListener("resize", () => {
    init();
    animate();
  });

  window.addEventListener("popstate", () => {
    window.location.reload();
  });
});
</script>

<style scoped>
@import "../assets/css/mainPage.css";

.mainpage_footer {
  margin: 0;
}
</style>
