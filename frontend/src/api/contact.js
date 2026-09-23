import api from './index'

/**
 * 发送联系表单消息到后端
 * @param {Object} formData - { name, email, subject, content }
 */
export function sendContact(formData) {
  return api.post('/contact', {
    name: formData.name,
    email: formData.email,
    subject: formData.subject,
    content: formData.content
  })
}
