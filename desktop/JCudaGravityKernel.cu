extern "C"
__global__ void grav(int n, double G,
	double *mass, double *posX, double *posY,
	double *rForceX, double *rForceY)
{
    int i = blockIdx.x * blockDim.x + threadIdx.x;
    int j = blockIdx.y * blockDim.y + threadIdx.y;
    if (i < n && j < n && i != j)
    {
        double relX = posX[j] - posX[i];
        double relY = posY[j] - posY[i];
        
        double dist2 = relX * relX + relY * relY;
        
        double scl = G * mass[i] * mass[j] * rsqrt(dist2 * dist2 * dist2);
        
        rForceX[i] += relX * scl;
        rForceY[i] += relY * scl;
    }

}
