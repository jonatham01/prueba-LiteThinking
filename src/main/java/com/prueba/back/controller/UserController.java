package com.prueba.back.controller;

import com.prueba.back.dto.AuthenticationResponse;
import com.prueba.back.persistance.entity.User;
import com.prueba.back.persistance.entity.UserLogin;
import com.prueba.back.persistance.repository.UserRepository;
import com.prueba.back.persistance.validator.ApiUnProcessableEntity;
import com.prueba.back.persistance.validator.UserValidator;
import com.prueba.back.security.JWTSecurity;
import com.prueba.back.service.EncryptServiceImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private EncryptServiceImpl encryptService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JWTSecurity jwtSecurity;

    @Autowired
    private UserValidator userValidator;



    @GetMapping("/pass/autenticated")
    public ResponseEntity<String> getpass() {
        return new ResponseEntity<>(encryptService.encryptPassword("hello"), HttpStatus.OK);

    }

    @PostMapping("/save/autenticated")
    public  ResponseEntity save(@RequestBody User user) throws ApiUnProcessableEntity {
        this.userValidator.validator(user);
        user.setPassword(encryptService.encryptPassword(user.getPassword()));
        return new ResponseEntity<>(userRepository.save(user),HttpStatus.CREATED);
    }
    @GetMapping("/all")
    public ResponseEntity getAll(){
        return new ResponseEntity<>(userRepository.findAll(),HttpStatus.OK);
    }


    @GetMapping("/id/{id}")
    public ResponseEntity getById(@PathVariable("id") int id){
        return userRepository.findById(id)
                .map(users -> new ResponseEntity<>(users,HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

   @GetMapping("/nombre/{name}")
    public ResponseEntity getByName(@PathVariable("name") String name){
        try{
            return new ResponseEntity<>(userRepository.findByUserName(name),HttpStatus.OK);

        }catch (Exception e){
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity delete(@PathVariable ("id") int id){
        try{
            userRepository.deleteById(id);
            return new ResponseEntity(HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

    }
    @PutMapping("/{id}")
    public ResponseEntity update(@PathVariable("id") int id, @RequestBody User data){
        return userRepository.findById(id)
                .map(user -> {
                    user.setUserName(data.getUserName());
                    user.setLastName(data.getLastName());
                    user.setEmail(data.getEmail());
                    user.setPassword(encryptService.encryptPassword(data.getPassword()));
                    userRepository.save(user);
                    return   new ResponseEntity<>(user,HttpStatus.OK);
                        }
                ).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/login/{id}/autenticated")
    public ResponseEntity login(@PathVariable("id") int id,@RequestBody UserLogin data){
        return userRepository.findById(id)
                .map(user->{
                    Boolean condicion= encryptService.verifyPassword(data.getPassword(),user.getPassword() );
                    if(!condicion) { return new ResponseEntity(HttpStatus.NOT_FOUND);}
                    else   {
                        UserDetails userDetails= userDetailsService.loadUserByUsername("username");
                        String jwt= jwtSecurity.generate(userDetails);
                        return  new ResponseEntity<>(new AuthenticationResponse(jwt),HttpStatus.OK);
                    }
                }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}