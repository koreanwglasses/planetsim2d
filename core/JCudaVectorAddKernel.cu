extern "C"
__global__ void add(int n, float *ax, float *ay, float *bx, float *by, float *sumx, float *sumy)
{
    int i = blockIdx.x * blockDim.x + threadIdx.x;
    if (i<n)
    {
        sumx[i] = ax[i] + bx[i];
        sumy[i] = ay[i] + by[i];
    }

}
