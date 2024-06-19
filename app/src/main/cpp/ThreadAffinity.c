#include "ThreadAffinity.h"
#include <jni.h>
#include <pthread.h>
#include <unistd.h>
#include <android/log.h>
#include <errno.h>

#define LOG_TAG "ThreadAffinity"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#ifdef __ANDROID__
#include <linux/unistd.h>
#include <sys/syscall.h>

typedef struct {
    unsigned long __bits[1024 / (8 * sizeof(unsigned long))];
} cpu_set_t;

#define CPU_ZERO(cpusetp) \
    memset((cpusetp), 0, sizeof(cpu_set_t))

#define CPU_SET(cpu, cpusetp) \
    ((cpusetp)->__bits[(cpu) / (8 * sizeof(unsigned long))] |= (1UL << ((cpu) % (8 * sizeof(unsigned long)))))

int sched_setaffinity(pid_t pid, size_t cpusetsize, const cpu_set_t *mask) {
    return syscall(__NR_sched_setaffinity, pid, cpusetsize, mask);
}

int pthread_setaffinity_np(pthread_t thread, size_t cpusetsize, const cpu_set_t *cpuset) {
    return sched_setaffinity(thread, cpusetsize, cpuset);
}
#endif

JNIEXPORT void JNICALL Java_com_seu_pacote_ThreadAffinity_setThreadAffinity(JNIEnv *env, jobject obj, jint cpu_id) {
    cpu_set_t cpuset;
    CPU_ZERO(&cpuset);
    if (cpu_id == -1) {
        // Adiciona todos os CPUs disponíveis
        int num_cpus = sysconf(_SC_NPROCESSORS_ONLN);
        for (int i = 0; i < num_cpus; i++) {
            CPU_SET(i, &cpuset);
        }
    } else {
        CPU_SET(cpu_id, &cpuset);
    }
    pthread_t current_thread = pthread_self();
    int result = pthread_setaffinity_np(current_thread, sizeof(cpu_set_t), &cpuset);
    if (result != 0) {
        LOGE("Erro ao definir afinidade da CPU: %d", errno);
    }
}

