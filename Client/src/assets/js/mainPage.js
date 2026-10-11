export default {
  mounted() {
    this.$nextTick(() => {
      const canvas = document.querySelector("canvas");
      const ctx = canvas.getContext("2d");
      const dpr = window.devicePixelRatio;

      let canvasWidth;
      let canvasHeight;

      const randomNumBetween = (min, max) => {
        return Math.random() * (max - min + 1) + min;
      };
      let particles;

      let interval = 1000 / 60;
      let now, delta;
      let then = Date.now();

      const feGaussianBlur = document.querySelector("feGaussianBlur");
      const feColorMatrix = document.querySelector("feColorMatrix");

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
          this.y += this.vy;
        }
        draw() {
          ctx.beginPath();
          ctx.arc(this.x, this.y, this.radius, 0, (Math.PI / 180) * 360);
          ctx.fillStyle = "white";
          // #166ADC
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
          const y = randomNumBetween(0, canvasHeight);
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
        if (a > 800) {
          if (b < 500) {
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
          if (particle.y - particle.radius > canvasHeight) {
            particle.y = 900 - a;
            if (a < 900) {
              a++;
            }
            // particle.x = randomNumBetween(0, canvasWidth);
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
      });
    });
  },
};
