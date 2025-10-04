cmake_minimum_required(VERSION 3.22.1)

project("aion_native")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(${CMAKE_PROJECT_NAME} SHARED
    native-lib.cpp
    image_processor.cpp
)

find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)
find_library(android-lib android)

target_link_libraries(${CMAKE_PROJECT_NAME}
    ${log-lib}
    ${jnigraphics-lib}
    ${android-lib}
)
