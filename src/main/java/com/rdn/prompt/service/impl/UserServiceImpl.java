package com.rdn.prompt.service.impl;

import com.rdn.prompt.auth.RoleEnum;
import com.rdn.prompt.common.ErrorCode;
import com.rdn.prompt.entity.User;
import com.rdn.prompt.entity.dto.RegistryDTO;
import com.rdn.prompt.entity.vo.UserVO;
import com.rdn.prompt.service.UserService;
import com.rdn.prompt.util.ApiBaseResponse;
import com.rdn.prompt.util.JwtUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    MongoTemplate mongoTemplate;



    @Override
    public User getById(String UserId) {
        return mongoTemplate.findById(UserId, User.class);
    }

    @Override
    public User getByName(String UserName) {
        return mongoTemplate.findOne(Query.query(Criteria.where("name").is(UserName)), User.class);
    }

    @Override
    public ApiBaseResponse login(RegistryDTO userDTO) {
        Query query = new Query(Criteria.where("username").is(userDTO.getUsername()).and("password").is(userDTO.getHashedPassword()));
        User user = mongoTemplate.findOne(query, User.class);
        if(user == null){
           return ApiBaseResponse.error(ErrorCode.USER_NOT_FOUND);
        }

        // 发放jwt token
        String token = JwtUtil.generateToken(user);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        userVO.setToken(token);

        return ApiBaseResponse.success(userVO);
    }

    @Override
    public ApiBaseResponse register(RegistryDTO userDTO) {
        Query query = new Query().addCriteria(Criteria.where("username").is(userDTO.getUsername()));
        List<User> users = mongoTemplate.find(query, User.class);
        if(users!= null && users.size() > 0){
            return ApiBaseResponse.error(ErrorCode.USER_HAS_EXIST);
        }
        User user = User.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(userDTO.getHashedPassword())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .role(RoleEnum.USER)
                .build();
        mongoTemplate.save(user);
        return ApiBaseResponse.success();
    }

    @Override
    public ApiBaseResponse deleteByUserId(String userId) {
        User user = mongoTemplate.findById(userId, User.class);
        if(user == null){
            return ApiBaseResponse.error(ErrorCode.USER_NOT_FOUND);
        }
        log.warn("正在删除用户：{}", user);
        Query query = new Query().addCriteria(Criteria.where("_id").is(userId));
        mongoTemplate.findAllAndRemove(query,User.class);
        return ApiBaseResponse.success();
    }
}
