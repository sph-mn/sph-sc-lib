typedef struct {
    b32_s id;
    b8 module;
} status_t;
typedef b32_s status_i_t;
#define status_init status_t status
#define status_ii_init b32_t status
#define status_goto(module,id) status.module=module;status.id=id;goto error
#define status_return(module,id) return((status_t)({.module=module,.id=id};))
#define status_require(expression,cont) status=expression;if(!zero_p(status.id)){cont;}
#define status_require_goto(expression) status_require(expression,goto status)
#define status_require_return(expression) status_require(expression,return(status))
#define status_io_require(module,expression,cont) status.id=expression;if(!zero_p(status.id)){cont;}
#define status_io_require_goto(module,expression) status_io_require(module,expression,goto error)
#define status_io_require_return(module,expression) status_io_require(module,expression,return(status))
#define status_ii_require(module,expression,cont) status=expression;if(!zero_p(status)){cont;}
#define status_ii_require_goto(module,expression) status_ii_require(module,expression,goto error)
#define status_ii_require_return(module,expression) status_ii_require(module,expression,return(status))
#define status_success_p(a) (0==a.id)
#define status_success 0
