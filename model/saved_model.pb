i
íÄ
:
Add
x"T
y"T
z"T"
Ttype:
2	

ApplyGradientDescent
var"T

alpha"T

delta"T
out"T" 
Ttype:
2	"
use_lockingbool( 
x
Assign
ref"T

value"T

output_ref"T"	
Ttype"
validate_shapebool("
use_lockingbool(
R
BroadcastGradientArgs
s0"T
s1"T
r0"T
r1"T"
Ttype0:
2	
8
Const
output"dtype"
valuetensor"
dtypetype
^
Fill
dims"
index_type

value"T
output"T"	
Ttype"

index_typetype0:
2	
.
Identity

input"T
output"T"	
Ttype
p
MatMul
a"T
b"T
product"T"
transpose_abool( "
transpose_bbool( "
Ttype:
	2

Mean

input"T
reduction_indices"Tidx
output"T"
	keep_dimsbool( " 
Ttype:
2	"
Tidxtype0:
2	
e
MergeV2Checkpoints
checkpoint_prefixes
destination_prefix"
delete_old_dirsbool(
=
Mul
x"T
y"T
z"T"
Ttype:
2	
.
Neg
x"T
y"T"
Ttype:

2	

NoOp
M
Pack
values"T*N
output"T"
Nint(0"	
Ttype"
axisint 
C
Placeholder
output"dtype"
dtypetype"
shapeshape:

RandomStandardNormal

shape"T
output"dtype"
seedint "
seed2int "
dtypetype:
2"
Ttype:
2	
>
RealDiv
x"T
y"T
z"T"
Ttype:
2	
[
Reshape
tensor"T
shape"Tshape
output"T"	
Ttype"
Tshapetype0:
2	
o
	RestoreV2

prefix
tensor_names
shape_and_slices
tensors2dtypes"
dtypes
list(type)(0
l
SaveV2

prefix
tensor_names
shape_and_slices
tensors2dtypes"
dtypes
list(type)(0
H
ShardedFilename
basename	
shard

num_shards
filename
1
Square
x"T
y"T"
Ttype:

2	
N

StringJoin
inputs*N

output"
Nint(0"
	separatorstring 
:
Sub
x"T
y"T
z"T"
Ttype:
2	

Sum

input"T
reduction_indices"Tidx
output"T"
	keep_dimsbool( " 
Ttype:
2	"
Tidxtype0:
2	
c
Tile

input"T
	multiples"
Tmultiples
output"T"	
Ttype"

Tmultiplestype0:
2	
s

VariableV2
ref"dtype"
shapeshape"
dtypetype"
	containerstring "
shared_namestring "serve*1.10.02v1.10.0-0-g656e7a2b34ĄS
V
inputPlaceholder*
dtype0*
_output_shapes

:d*
shape
:d
\
PlaceholderPlaceholder*
shape
:*
dtype0*
_output_shapes

:
d
random_normal/shapeConst*
dtype0*
_output_shapes
:*
valueB"d      
W
random_normal/meanConst*
valueB
 *    *
dtype0*
_output_shapes
: 
Y
random_normal/stddevConst*
valueB
 *  ?*
dtype0*
_output_shapes
: 

"random_normal/RandomStandardNormalRandomStandardNormalrandom_normal/shape*
_output_shapes

:d*
seed2 *

seed *
T0*
dtype0
{
random_normal/mulMul"random_normal/RandomStandardNormalrandom_normal/stddev*
T0*
_output_shapes

:d
d
random_normalAddrandom_normal/mulrandom_normal/mean*
_output_shapes

:d*
T0
z
weight
VariableV2*
shared_name *
dtype0*
_output_shapes

:d*
	container *
shape
:d

weight/AssignAssignweightrandom_normal*
validate_shape(*
_output_shapes

:d*
use_locking(*
T0*
_class
loc:@weight
c
weight/readIdentityweight*
_class
loc:@weight*
_output_shapes

:d*
T0
_
random_normal_1/shapeConst*
valueB:*
dtype0*
_output_shapes
:
Y
random_normal_1/meanConst*
valueB
 *    *
dtype0*
_output_shapes
: 
[
random_normal_1/stddevConst*
valueB
 *  ?*
dtype0*
_output_shapes
: 

$random_normal_1/RandomStandardNormalRandomStandardNormalrandom_normal_1/shape*
dtype0*
_output_shapes
:*
seed2 *

seed *
T0
}
random_normal_1/mulMul$random_normal_1/RandomStandardNormalrandom_normal_1/stddev*
_output_shapes
:*
T0
f
random_normal_1Addrandom_normal_1/mulrandom_normal_1/mean*
T0*
_output_shapes
:
p
bias
VariableV2*
shape:*
shared_name *
dtype0*
_output_shapes
:*
	container 

bias/AssignAssignbiasrandom_normal_1*
validate_shape(*
_output_shapes
:*
use_locking(*
T0*
_class
	loc:@bias
Y
	bias/readIdentitybias*
T0*
_class
	loc:@bias*
_output_shapes
:
s
MatMulMatMulinputweight/read*
_output_shapes

:*
transpose_a( *
transpose_b( *
T0
F
addAddMatMul	bias/read*
T0*
_output_shapes

:
@
outputIdentityadd*
_output_shapes

:*
T0
E
subSubaddPlaceholder*
T0*
_output_shapes

:
>
SquareSquaresub*
T0*
_output_shapes

:
V
ConstConst*
_output_shapes
:*
valueB"       *
dtype0
Y
MeanMeanSquareConst*
_output_shapes
: *
	keep_dims( *

Tidx0*
T0
R
gradients/ShapeConst*
_output_shapes
: *
valueB *
dtype0
X
gradients/grad_ys_0Const*
valueB
 *  ?*
dtype0*
_output_shapes
: 
o
gradients/FillFillgradients/Shapegradients/grad_ys_0*
T0*

index_type0*
_output_shapes
: 
r
!gradients/Mean_grad/Reshape/shapeConst*
valueB"      *
dtype0*
_output_shapes
:

gradients/Mean_grad/ReshapeReshapegradients/Fill!gradients/Mean_grad/Reshape/shape*
T0*
Tshape0*
_output_shapes

:
j
gradients/Mean_grad/ConstConst*
valueB"      *
dtype0*
_output_shapes
:

gradients/Mean_grad/TileTilegradients/Mean_grad/Reshapegradients/Mean_grad/Const*
_output_shapes

:*

Tmultiples0*
T0
`
gradients/Mean_grad/Const_1Const*
valueB
 *   @*
dtype0*
_output_shapes
: 

gradients/Mean_grad/truedivRealDivgradients/Mean_grad/Tilegradients/Mean_grad/Const_1*
T0*
_output_shapes

:
~
gradients/Square_grad/ConstConst^gradients/Mean_grad/truediv*
valueB
 *   @*
dtype0*
_output_shapes
: 
k
gradients/Square_grad/MulMulsubgradients/Square_grad/Const*
T0*
_output_shapes

:

gradients/Square_grad/Mul_1Mulgradients/Mean_grad/truedivgradients/Square_grad/Mul*
_output_shapes

:*
T0
c
gradients/sub_grad/NegNeggradients/Square_grad/Mul_1*
T0*
_output_shapes

:
b
#gradients/sub_grad/tuple/group_depsNoOp^gradients/Square_grad/Mul_1^gradients/sub_grad/Neg
Ó
+gradients/sub_grad/tuple/control_dependencyIdentitygradients/Square_grad/Mul_1$^gradients/sub_grad/tuple/group_deps*
T0*.
_class$
" loc:@gradients/Square_grad/Mul_1*
_output_shapes

:
Ë
-gradients/sub_grad/tuple/control_dependency_1Identitygradients/sub_grad/Neg$^gradients/sub_grad/tuple/group_deps*
_output_shapes

:*
T0*)
_class
loc:@gradients/sub_grad/Neg
i
gradients/add_grad/ShapeConst*
valueB"      *
dtype0*
_output_shapes
:
d
gradients/add_grad/Shape_1Const*
valueB:*
dtype0*
_output_shapes
:
´
(gradients/add_grad/BroadcastGradientArgsBroadcastGradientArgsgradients/add_grad/Shapegradients/add_grad/Shape_1*2
_output_shapes 
:˙˙˙˙˙˙˙˙˙:˙˙˙˙˙˙˙˙˙*
T0
ś
gradients/add_grad/SumSum+gradients/sub_grad/tuple/control_dependency(gradients/add_grad/BroadcastGradientArgs*
T0*
_output_shapes
:*
	keep_dims( *

Tidx0

gradients/add_grad/ReshapeReshapegradients/add_grad/Sumgradients/add_grad/Shape*
_output_shapes

:*
T0*
Tshape0
ś
gradients/add_grad/Sum_1Sum+gradients/sub_grad/tuple/control_dependency*gradients/add_grad/BroadcastGradientArgs:1*
T0*
_output_shapes
: *
	keep_dims( *

Tidx0

gradients/add_grad/Reshape_1Reshapegradients/add_grad/Sum_1gradients/add_grad/Shape_1*
_output_shapes
:*
T0*
Tshape0
g
#gradients/add_grad/tuple/group_depsNoOp^gradients/add_grad/Reshape^gradients/add_grad/Reshape_1
Ń
+gradients/add_grad/tuple/control_dependencyIdentitygradients/add_grad/Reshape$^gradients/add_grad/tuple/group_deps*
T0*-
_class#
!loc:@gradients/add_grad/Reshape*
_output_shapes

:
Ó
-gradients/add_grad/tuple/control_dependency_1Identitygradients/add_grad/Reshape_1$^gradients/add_grad/tuple/group_deps*
_output_shapes
:*
T0*/
_class%
#!loc:@gradients/add_grad/Reshape_1
Ż
gradients/MatMul_grad/MatMulMatMul+gradients/add_grad/tuple/control_dependencyweight/read*
T0*
_output_shapes

:d*
transpose_a( *
transpose_b(
Ť
gradients/MatMul_grad/MatMul_1MatMulinput+gradients/add_grad/tuple/control_dependency*
T0*
_output_shapes

:d*
transpose_a(*
transpose_b( 
n
&gradients/MatMul_grad/tuple/group_depsNoOp^gradients/MatMul_grad/MatMul^gradients/MatMul_grad/MatMul_1
Ű
.gradients/MatMul_grad/tuple/control_dependencyIdentitygradients/MatMul_grad/MatMul'^gradients/MatMul_grad/tuple/group_deps*
_output_shapes

:d*
T0*/
_class%
#!loc:@gradients/MatMul_grad/MatMul
á
0gradients/MatMul_grad/tuple/control_dependency_1Identitygradients/MatMul_grad/MatMul_1'^gradients/MatMul_grad/tuple/group_deps*
T0*1
_class'
%#loc:@gradients/MatMul_grad/MatMul_1*
_output_shapes

:d
b
GradientDescent/learning_rateConst*
valueB
 *ŹĹ'7*
dtype0*
_output_shapes
: 
ú
2GradientDescent/update_weight/ApplyGradientDescentApplyGradientDescentweightGradientDescent/learning_rate0gradients/MatMul_grad/tuple/control_dependency_1*
use_locking( *
T0*
_class
loc:@weight*
_output_shapes

:d
í
0GradientDescent/update_bias/ApplyGradientDescentApplyGradientDescentbiasGradientDescent/learning_rate-gradients/add_grad/tuple/control_dependency_1*
_output_shapes
:*
use_locking( *
T0*
_class
	loc:@bias

GradientDescentNoOp1^GradientDescent/update_bias/ApplyGradientDescent3^GradientDescent/update_weight/ApplyGradientDescent
*
initNoOp^bias/Assign^weight/Assign
P

save/ConstConst*
valueB Bmodel*
dtype0*
_output_shapes
: 

save/StringJoin/inputs_1Const*
dtype0*
_output_shapes
: *<
value3B1 B+_temp_2b95590651e6410bb40d2891254f7cf4/part
u
save/StringJoin
StringJoin
save/Constsave/StringJoin/inputs_1*
N*
_output_shapes
: *
	separator 
Q
save/num_shardsConst*
value	B :*
dtype0*
_output_shapes
: 
k
save/ShardedFilename/shardConst"/device:CPU:0*
value	B : *
dtype0*
_output_shapes
: 

save/ShardedFilenameShardedFilenamesave/StringJoinsave/ShardedFilename/shardsave/num_shards"/device:CPU:0*
_output_shapes
: 
|
save/SaveV2/tensor_namesConst"/device:CPU:0*!
valueBBbiasBweight*
dtype0*
_output_shapes
:
v
save/SaveV2/shape_and_slicesConst"/device:CPU:0*
valueBB B *
dtype0*
_output_shapes
:

save/SaveV2SaveV2save/ShardedFilenamesave/SaveV2/tensor_namessave/SaveV2/shape_and_slicesbiasweight"/device:CPU:0*
dtypes
2
 
save/control_dependencyIdentitysave/ShardedFilename^save/SaveV2"/device:CPU:0*
T0*'
_class
loc:@save/ShardedFilename*
_output_shapes
: 
Ź
+save/MergeV2Checkpoints/checkpoint_prefixesPacksave/ShardedFilename^save/control_dependency"/device:CPU:0*
T0*

axis *
N*
_output_shapes
:

save/MergeV2CheckpointsMergeV2Checkpoints+save/MergeV2Checkpoints/checkpoint_prefixes
save/Const"/device:CPU:0*
delete_old_dirs(

save/IdentityIdentity
save/Const^save/MergeV2Checkpoints^save/control_dependency"/device:CPU:0*
_output_shapes
: *
T0

save/RestoreV2/tensor_namesConst"/device:CPU:0*!
valueBBbiasBweight*
dtype0*
_output_shapes
:
y
save/RestoreV2/shape_and_slicesConst"/device:CPU:0*
valueBB B *
dtype0*
_output_shapes
:
¤
save/RestoreV2	RestoreV2
save/Constsave/RestoreV2/tensor_namessave/RestoreV2/shape_and_slices"/device:CPU:0*
_output_shapes

::*
dtypes
2

save/AssignAssignbiassave/RestoreV2*
use_locking(*
T0*
_class
	loc:@bias*
validate_shape(*
_output_shapes
:

save/Assign_1Assignweightsave/RestoreV2:1*
use_locking(*
T0*
_class
loc:@weight*
validate_shape(*
_output_shapes

:d
8
save/restore_shardNoOp^save/Assign^save/Assign_1
-
save/restore_allNoOp^save/restore_shard"<
save/Const:0save/Identity:0save/restore_all (5 @F8"
trainable_variablesxv
;
weight:0weight/Assignweight/read:02random_normal:08
7
bias:0bias/Assignbias/read:02random_normal_1:08"
train_op

GradientDescent"
	variablesxv
;
weight:0weight/Assignweight/read:02random_normal:08
7
bias:0bias/Assignbias/read:02random_normal_1:08