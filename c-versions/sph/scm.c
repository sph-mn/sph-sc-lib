
#define scm_first SCM_CAR
#define scm_tail SCM_CDR
#define scm_c_define_procedure_c(scm_temp,name,required,optional,rest,c_function,documentation) scm_temp=scm_c_define_gsubr(name,required,optional,rest,c_function);scm_set_procedure_property_x(scm_temp,scm_from_locale_symbol("documentation"),scm_from_locale_string(documentation))
#define scm_c_list_each(list,e,body) while(!scm_is_null(list)){e=scm_first(list);body;list=scm_tail(list);}
#define scm_is_undefined(a) (SCM_UNDEFINED==a)
#define scm_false_if_undefined(a) (scm_is_undefined(a)?SCM_BOOL_F:a)
#define null_if_undefined(a) (scm_is_undefined(a)?0:a)
#define scm_if_undefined_expr(a,b,c) (scm_is_undefined(a)?b:c)
#define scm_if_undefined(a,b,c) if(scm_is_undefined(a)){b;}else{c;}
#define scm_is_list_false_or_undefined(a) (scm_is_true(scm_list_p(a))||(SCM_BOOL_F==a)||(SCM_UNDEFINED==a))
#define scm_is_integer_false_or_undefined(a) (scm_is_integer(a)||(SCM_BOOL_F==a)||(SCM_UNDEFINED==a))
SCM scm_c_bytevector_take(size_t size_octets,b8* a) {
    SCM r=scm_c_make_bytevector(size_octets);
    memcpy(SCM_BYTEVECTOR_CONTENTS(r),a,size_octets);
    return(r);
}
#define scm_c_error_create(name,data) scm_call_3(scm_error_create,scm_from_locale_symbol(__func__),(data?data:SCM_BOOL_F))
SCM scm_error_create;
SCM scm_error_p;
SCM scm_error_origin;
SCM scm_error_name;
SCM scm_error_data;
b0 init_scm() {
    SCM m=scm_c_resolve_module("sph error");
    scm_error_create=scm_variable_ref(scm_c_module_lookup(m,"error-create-p"));
    scm_error_origin=scm_variable_ref(scm_c_module_lookup(m,"error-origin"));
    scm_error_name=scm_variable_ref(scm_c_module_lookup(m,"error-name"));
    scm_error_data=scm_variable_ref(scm_c_module_lookup(m,"error-data"));
    scm_error_p=scm_variable_ref(scm_c_module_lookup(m,"error?"));
}
#define scm_c_local_error_init SCM local_error_origin;SCM local_error_name;SCM local_error_data
#define scm_c_local_error(i,d) local_error_origin=scm_from_locale_symbol(__func__);local_error_name=scm_from_locale_symbol(i);local_error_data=d;goto error
#define scm_c_local_error_create scm_call_3(scm_error_create,local_error_origin,local_error_name,(local_error_data?local_error_data:SCM_BOOL_F))
#define scm_c_local_define_malloc(variable_name,type) type* variable_name=malloc(sizeof(type));if(!variable_name){scm_c_local_error("memory",0);}
#define scm_c_local_define_malloc_and_size(variable_name,type,size) type* variable_name=malloc(size);if(!variable_name){scm_c_local_error("memory",0);}
#define scm_c_local_error_return return(scm_c_local_error_create)
#if local_error_assert_enable

#define scm_c_local_error_assert(name,expr) if(!expr){scm_c_local_error(name,0);}

#else

#define scm_c_local_error_assert(name,expr) null

#endif
#define scm_c_local_error_glibc(error_number) scm_c_local_error("glibc",scm_from_locale_string(strerror(error_number)))
#define scm_c_local_error_system scm_c_local_error("system",scm_from_locale_string(strerror(errno)))
#define scm_c_require_success_glibc(a) s=a;if((s<0)){scm_c_local_error_glibc(s);}
#define scm_c_require_success_system(a) if((a<0)){scm_c_local_error_system;}
