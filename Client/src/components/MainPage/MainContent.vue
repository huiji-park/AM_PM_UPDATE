<template>
  <div class="main_title_container">
    <div id="main-img-wrapper">
      <img
        width="600"
        src="../../assets/images/mainPage/team_image.png"
        alt="coding image"
      />
    </div>
    <div id="intro-main">
      <h1>AM_PM 에서</h1>
      <h1 class="marginb20">
        모두 <span class="color_point">ALL IN ONE</span>
      </h1>
      <div class="intro_main_container">
        <p>- 개발 산출물 관리</p>
        <p>- 실시간 팀 채팅</p>
        <p>- 개발 일정 GanttChat</p>
        <p>- 운영 문의 게시판</p>
      </div>
    </div>
  </div>
  <div class="title_text_container">
    <h1 class="title_text">
      🤯 개발 프로젝트를 시작했는데 관리할게 너무 많아! 🤯
    </h1>
  </div>
  <ul id="list-item-wrapper">
    <li class="list-item">프로젝트관리</li>
    <li class="list-item">요구사항정의서</li>
    <li class="list-item">개발</li>
    <li class="list-item">팀채팅</li>
    <li class="list-item">ERD</li>
    <li class="list-item">공지사항</li>
    <li class="list-item">간트차트</li>
    <li class="list-item">페이지명세서</li>
    <li class="list-item">아키텍쳐</li>
    <li class="list-item">개발일정관리</li>
    <li class="list-item">간편로그인</li>
    <li class="list-item">맴버관리</li>
    <li class="list-item">권한관리</li>
    <li class="list-item">명세서관리</li>
    <li class="list-item">공유문서</li>
    <li class="list-item">커스텀명세관리</li>
    <li class="list-item">파일첨부</li>
    <li class="list-item">데이터베이스</li>
    <li class="list-item">차트수정</li>
    <li class="list-item">커뮤니티</li>
    <li class="list-item">서비스문의</li>
    <li class="list-item">프로필수정</li>
    <li class="list-item">유스케이스</li>
    <li class="list-item">엑셀문서</li>
    <li class="list-item">관리자문의</li>
    <li class="list-item">프로젝트매니져</li>
    <li class="list-item">개발자</li>
    <li class="list-item">협업</li>
  </ul>
  <main>
    <div class="ansewer_container">
      <h1>AM_PM</h1>
      <h1>서비스 한 곳에서</h1>
      <h1>모두 해결! 🎉</h1>
    </div>
    <section id="panel1-img">
      <img
        id="flying-santa-image"
        src="../../assets/images/mainPage/santa_flying.png"
        alt="santa flying"
      />
    </section>
    <div class="arrow_container">
      <img
        id="down-arrow-icon"
        src="../../assets/images/mainPage/down-arrow.png"
        alt="down arrow icon"
      />
      <router-link to="/login">
        <h1 id="go_login_text">지금 바로 시작하기</h1>
      </router-link>
    </div>
  </main>
</template>

<script setup>
import { onMounted } from "vue";

onMounted(() => {
  const listStyleChangeStartY = 1600;
  const listStyleChangeEndY = 3400;

  const listItems = document.querySelectorAll(".list-item");

  const division =
    (listStyleChangeEndY - listStyleChangeStartY) / listItems.length;

  const panel1Img = document.getElementById("panel1-img");
  const flyingSantaImage = document.getElementById("flying-santa-image");

  window.addEventListener("scroll", () => {
    if (document.getElementById("on"))
      document.getElementById("on").removeAttribute("id");
    if (
      window.scrollY > listStyleChangeStartY &&
      window.scrollY < listStyleChangeEndY
    ) {
      const targetIndex = Math.round(
        (window.scrollY - listStyleChangeStartY) / division
      );

      if (listItems[targetIndex]) listItems[targetIndex].id = "on";
    }

    const scrollYBottom =
      window.scrollY + document.documentElement.clientHeight;

    if (
      scrollYBottom > panel1Img.offsetTop &&
      scrollYBottom < panel1Img.offsetTop + panel1Img.offsetHeight + 100
    ) {
      const translateX =
        900 -
        (900 * 1.3 * (scrollYBottom - panel1Img.offsetTop)) /
          (panel1Img.offsetHeight + 100);
      const translateY =
        -13 +
        (13 * (scrollYBottom - panel1Img.offsetTop)) /
          (panel1Img.offsetHeight + 100);

      const rotationDegree =
        27 -
        (27 * 1.7 * (scrollYBottom - panel1Img.offsetTop)) /
          (panel1Img.offsetHeight + 100);

      flyingSantaImage.style.transform = `translate(${translateX}px, ${translateY}px) rotate(${
        rotationDegree - 30
      }deg)`;
    }
  });
});
</script>

<style scoped>
@import "../../assets/css/mainContent.css";
</style>
