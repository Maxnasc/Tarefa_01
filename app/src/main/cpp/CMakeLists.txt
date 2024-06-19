cmake_minimum_required(VERSION 3.10.2)

project("tarefa_01")

add_library(
        ThreadAffinity
        SHARED
        ThreadAffinity.c)

find_library(
        log-lib
        log)

target_link_libraries(
        ThreadAffinity
        ${log-lib})
