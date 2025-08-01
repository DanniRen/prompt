package com.rdn.prompt.entity.dto;

import com.rdn.prompt.constants.MessageConstants;
import com.rdn.prompt.constants.RegexConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.mindrot.jbcrypt.BCrypt;

@ApiModel("用户注册对象")
@Data
public class RegistryDTO {

    @ApiModelProperty(value = "用户名", notes = "最短3个字符，最长32个字符", required = true)
    @NotNull(message = MessageConstants.PARAMS_NOT_NULL)
    @Size(min = 3, max = 32, message = MessageConstants.PARAMS_LENGTH_REQUIRED)
    @Pattern(regexp = RegexConstants.NUM_WORD_REG, message = MessageConstants.PARAMS_FORMAT_ERROR)
    private String username;

    @ApiModelProperty(value = "邮箱", required = true)
    @NotNull(message = MessageConstants.PARAMS_NOT_NULL)
    @Pattern(regexp = RegexConstants.EMAIL_REGEX, message = MessageConstants.PARAMS_FORMAT_ERROR)
    private String email;

    @ApiModelProperty(value = "密码", notes = "3-32个字符，允许字母、数字和@$!%*?&", required = true)
    @NotNull(message = MessageConstants.PARAMS_NOT_NULL)
    @Size(min = 3, max = 32, message = MessageConstants.PARAMS_LENGTH_REQUIRED)
    @Pattern(regexp = RegexConstants.PASSWORD_REGEX, message = MessageConstants.PARAMS_FORMAT_ERROR)
    private String password;

    public String getHashedPassword() {
        if (password == null) {
            return "";
        }
        String hashpw = BCrypt.hashpw(password, BCrypt.gensalt(12));
        return hashpw;
    }

    public boolean verifyPassword(String hashpw) {
        if (password == null || hashpw == null) {
            return false;
        }
        return BCrypt.checkpw(password, hashpw);
    }

    public static void main(String[] args) {
        RegistryDTO dto = new RegistryDTO();
        dto.setUsername("user");
        dto.setEmail("user@example.com");
        dto.setPassword("user");
        System.out.println(dto.getHashedPassword());
    }
}
