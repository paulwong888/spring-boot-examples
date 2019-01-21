package com.andy.mongodb.service;

import com.andy.mongodb.entity.User;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *
 * @author leone
 * @since 2019-01-10
 **/
@Slf4j
@Service
public class MongoService {

    @Resource
    private MongoTemplate mongoTemplate;


    /**
     * 新增
     *
     * @param user
     * @return
     */
    public User save(User user) {
        mongoTemplate.save(user);
        return user;
    }


    /**
     * 列表
     *
     * @return
     */
    public List<User> list() {
        return mongoTemplate.findAll(User.class);
    }


    /**
     * 根据名称查找
     *
     * @param account
     * @return
     */
    public User findByName(String account) {
        Query query = new Query(Criteria.where("account").is(account));
        return mongoTemplate.findOne(query, User.class);
    }

    /**
     * 更新
     *
     * @param user
     */
    public void update(User user) {
        Query query = new Query(Criteria.where("userId").is(user.getUserId()));
        Update update = new Update()
                .set("description", user.getDescription())
                .set("password", user.getPassword())
                .set("age", user.getAge())
                .set("deleted", user.isDeleted())
                .set("createTime", user.getCreateTime())
                .set("account", user.getAccount());
        // 更新查询返回结果集的第一条
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        // 更新查询返回结果集的所有
        // mongoTemplate.updateMulti(query,update,User.class);
    }


    /**
     * 删除
     *
     * @param userId
     */
    public void delete(Integer userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        mongoTemplate.remove(query, User.class);
    }


}
