import axios from "axios";
import { authApi, apiInstance, authFileApi } from "./config";

const api = async (url, method, data) => {
  return (
    await axios({
      method: method,
      url,
      data,
    }).catch((e) => {
      console.log(e);
    })
  ).data;
};

export { api };

export const getMyInfoReq = async () => {
  const response = await authApi.get("/api/user");
  return response;
};

export const editMyInfoReq = async (updateData) => {
  const response = await authApi.put("/api/user", updateData);
  return response;
};

export const getNoti = async (projectId) => {
  const response = await apiInstance.get(`/api/notice/${projectId}`);
  return response;
};

export const addNoti = async (formdata) => {
  const response = await authApi.post("/api/notice", formdata);
  return response;
};

export const deleteNoti = async (noticeId) => {
  const response = await apiInstance.delete(`/api/notice/${noticeId}`);
  return response;
};

export const editNoti = async (noticeId, formdata) => {
  const response = await apiInstance.put(`/api/notice/${noticeId}`, formdata);
  return response;
};

// 간트차트
export const getProject = async (projectId) => {
  const response = await apiInstance.get(`/api/project/${projectId}`);
  return response;
};

export const getTasks = async (projectId) => {
  const response = await apiInstance.get(`/api/task/${projectId}`);
  return response;
};

export const addTasks = async (formData) => {
  const response = await authApi.post(`/api/task`, formData);
  return response;
};

export const deleteTask = async (taskId) => {
  const response = await apiInstance.delete(`/api/task/${taskId}`);
  return response;
};

export const editTask = async (taskId, updateData) => {
  const response = await apiInstance.put(`/api/task/${taskId}`, updateData);
  return response;
};

// 파일 전송
export const editProfile = async (formData) => {
  const response = await authFileApi.put(`/api/profile_img`, formData);
  return response;
};
