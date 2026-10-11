import axios from "axios";

const apiConfig = {
  baseURL: "http://localhost:8090/",
  headers: {
    "Content-Type": "application/json",
  },
};

const apiFileConfig = {
  baseURL: "http://localhost:8090/",
  headers: {
    "Content-Type": "multipart/form-data",
    accept: "application/json",
  },
};

export const authApi = axios.create(apiConfig);
export const apiInstance = axios.create(apiConfig);
export const authFileApi = axios.create(apiFileConfig);

// 인터 셉터
authApi.interceptors.request.use((config) => {
  config.headers.Authorization = getLocalStorageToken();
  return config;
});

authFileApi.interceptors.request.use((config) => {
  config.headers.Authorization = getLocalStorageToken();
  return config;
});

const getLocalStorageToken = () => {
  const token = sessionStorage.getItem("access-token");
  return token;
};

export const expireToken = async (err, retryFunction, data) => {
  if (err.response.status === 401) {
    console.log("토큰 만료");
    try {
      const response = await axios.get("http://localhost:8090/api/rtoken", {
        headers: {
          RefreshToken: sessionStorage.getItem("refresh-token"),
          Authorization: sessionStorage.getItem("access-token"),
        },
      });

      console.log(response);

      if (response.status === 200) {
        console.log("토큰 재발급");
        console.log(response.headers.authorization);
        sessionStorage.setItem("access-token", response.headers.authorization);
        await retryFunction(data);
      } else {
        console.log("토큰 재발급 실패");
      }
    } catch (err) {
      console.error(err);
    }
  }
};
